package no.nav.helse.flex.bigquery

import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.BigQueryException
import com.google.cloud.bigquery.Field
import com.google.cloud.bigquery.Schema
import com.google.cloud.bigquery.StandardSQLTypeName
import com.google.cloud.bigquery.StandardTableDefinition
import com.google.cloud.bigquery.TableDefinition
import com.google.cloud.bigquery.TableId
import com.google.cloud.bigquery.TableInfo
import no.nav.helse.flex.logger
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@Profile("createtable")
class TableCreator(
    val bigQuery: BigQuery
) {

    val log = logger()

    @PostConstruct
    fun initTestTabell() {
        val tableName = "soknadtest"
        val schema: Schema = Schema.of(
            Field.of("soknadid", StandardSQLTypeName.STRING),
            Field.of("status", StandardSQLTypeName.STRING),
            Field.of("opprettet", StandardSQLTypeName.TIMESTAMP)
        )
        createTable(tableName, schema)
    }

    fun createTable(tableName: String, schema: Schema) {
        try {
            // Initialize client that will be used to send requests. This client only needs to be created
            // once, and can be reused for multiple requests.
            val tableId = TableId.of(dataset, tableName)
            val tableDefinition: TableDefinition = StandardTableDefinition.of(schema)
            val tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build()
            bigQuery.create(tableInfo)
            log.info("Table $tableName created successfully")
        } catch (e: BigQueryException) {
            log.error("Table was not created.,", e)
            throw e
        }
    }
}
