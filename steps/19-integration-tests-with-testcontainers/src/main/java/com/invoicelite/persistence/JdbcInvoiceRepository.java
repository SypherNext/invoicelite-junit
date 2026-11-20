package com.invoicelite.persistence;

import com.invoicelite.invoice.Invoice;
import com.invoicelite.invoice.InvoiceStatus;
import com.invoicelite.invoice.LineItem;
import com.invoicelite.money.Money;
import com.invoicelite.service.InvoiceRepository;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Stores invoices in PostgreSQL with plain JDBC.
 */
public final class JdbcInvoiceRepository implements InvoiceRepository {

    private static final String UPSERT_INVOICE = """
            insert into invoices (number, customer, country_code, currency, status, issue_date, due_date)
            values (?, ?, ?, ?, ?, ?, ?)
            on conflict (number) do update set
                status = excluded.status, issue_date = excluded.issue_date, due_date = excluded.due_date""";
    private static final String DELETE_LINES = "delete from invoice_lines where invoice_number = ?";
    private static final String INSERT_LINE = """
            insert into invoice_lines (invoice_number, position, sku, description, unit_price, quantity)
            values (?, ?, ?, ?, ?, ?)""";
    private static final String SELECT_INVOICE = "select * from invoices where number = ?";
    private static final String SELECT_NUMBERS_BY_STATUS = "select number from invoices where status = ? order by number";
    private static final String SELECT_LINES = "select * from invoice_lines where invoice_number = ? order by position";

    private final DataSource dataSource;

    public JdbcInvoiceRepository(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    /** Creates the tables from schema.sql if they do not exist yet. */
    public void createSchema() {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            for (String sql : readSchema().split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create the schema", e);
        }
    }

    @Override
    public void save(Invoice invoice) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                upsertInvoice(connection, invoice);
                replaceLines(connection, invoice);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not save invoice " + invoice.number(), e);
        }
    }

    @Override
    public Optional<Invoice> findByNumber(String number) {
        try (Connection connection = dataSource.getConnection()) {
            return load(connection, number);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load invoice " + number, e);
        }
    }

    @Override
    public List<Invoice> findByStatus(InvoiceStatus status) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement select = connection.prepareStatement(SELECT_NUMBERS_BY_STATUS)) {
            select.setString(1, status.name());
            List<Invoice> invoices = new ArrayList<>();
            try (ResultSet rows = select.executeQuery()) {
                while (rows.next()) {
                    load(connection, rows.getString("number")).ifPresent(invoices::add);
                }
            }
            return invoices;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load " + status + " invoices", e);
        }
    }

    private static void upsertInvoice(Connection connection, Invoice invoice) throws SQLException {
        try (PreparedStatement upsert = connection.prepareStatement(UPSERT_INVOICE)) {
            upsert.setString(1, invoice.number());
            upsert.setString(2, invoice.customer());
            upsert.setString(3, invoice.countryCode());
            upsert.setString(4, invoice.currency().getCurrencyCode());
            upsert.setString(5, invoice.status().name());
            upsert.setDate(6, invoice.issueDate().map(Date::valueOf).orElse(null));
            upsert.setDate(7, invoice.dueDate().map(Date::valueOf).orElse(null));
            upsert.executeUpdate();
        }
    }

    private static void replaceLines(Connection connection, Invoice invoice) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement(DELETE_LINES);
             PreparedStatement insert = connection.prepareStatement(INSERT_LINE)) {
            delete.setString(1, invoice.number());
            delete.executeUpdate();
            List<LineItem> lines = invoice.lines();
            for (int position = 0; position < lines.size(); position++) {
                LineItem line = lines.get(position);
                insert.setString(1, invoice.number());
                insert.setInt(2, position);
                insert.setString(3, line.sku());
                insert.setString(4, line.description());
                insert.setBigDecimal(5, line.unitPrice().amount());
                insert.setInt(6, line.quantity());
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private static Optional<Invoice> load(Connection connection, String number) throws SQLException {
        try (PreparedStatement select = connection.prepareStatement(SELECT_INVOICE)) {
            select.setString(1, number);
            try (ResultSet row = select.executeQuery()) {
                if (!row.next()) {
                    return Optional.empty();
                }
                Currency currency = Currency.getInstance(row.getString("currency"));
                return Optional.of(Invoice.restore(
                        row.getString("number"),
                        row.getString("customer"),
                        row.getString("country_code"),
                        currency,
                        loadLines(connection, number, currency),
                        InvoiceStatus.valueOf(row.getString("status")),
                        toLocalDate(row.getDate("issue_date")),
                        toLocalDate(row.getDate("due_date"))));
            }
        }
    }

    private static List<LineItem> loadLines(Connection connection, String number, Currency currency) throws SQLException {
        try (PreparedStatement select = connection.prepareStatement(SELECT_LINES)) {
            select.setString(1, number);
            List<LineItem> lines = new ArrayList<>();
            try (ResultSet rows = select.executeQuery()) {
                while (rows.next()) {
                    lines.add(new LineItem(
                            rows.getString("sku"),
                            rows.getString("description"),
                            new Money(rows.getBigDecimal("unit_price"), currency),
                            rows.getInt("quantity")));
                }
            }
            return lines;
        }
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static String readSchema() {
        try (InputStream in = JdbcInvoiceRepository.class.getResourceAsStream("/schema.sql")) {
            return new String(Objects.requireNonNull(in, "schema.sql not found").readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
