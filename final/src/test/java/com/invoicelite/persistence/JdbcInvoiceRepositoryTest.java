package com.invoicelite.persistence;

import com.invoicelite.service.InvoiceRepository;
import com.invoicelite.service.InvoiceRepositoryContract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Runs the repository contract against a real PostgreSQL in a throwaway Docker container.
 */
@Tag("integration")
@Testcontainers
class JdbcInvoiceRepositoryTest extends InvoiceRepositoryContract {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

    private PGSimpleDataSource dataSource;

    @BeforeEach
    void emptyDatabase() throws SQLException {
        dataSource = new PGSimpleDataSource();
        dataSource.setURL(POSTGRES.getJdbcUrl());
        dataSource.setUser(POSTGRES.getUsername());
        dataSource.setPassword(POSTGRES.getPassword());
        new JdbcInvoiceRepository(dataSource).createSchema();
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("truncate invoices cascade");
        }
    }

    @Override
    protected InvoiceRepository repository() {
        return new JdbcInvoiceRepository(dataSource);
    }
}
