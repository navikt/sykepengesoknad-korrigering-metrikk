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
        log.info("Kjører postconstruct i table creator")

        createTable(
            tableName = korrigerteSporsmalTableName,
            schema = Schema.of(
                Field.newBuilder("sykepengesoknadId", StandardSQLTypeName.STRING)
                    .setDescription("Id på sykepengesøknad som korrigerer").build(),
                Field.newBuilder("opprettet", StandardSQLTypeName.TIMESTAMP)
                    .setDescription("Tidspunktet denne raden ble opprettet i bigquery").build(),
                Field.newBuilder("korrigeringSendt", StandardSQLTypeName.TIMESTAMP)
                    .setDescription("Tidspunktet korrigeringen ble sendt").build(),
                Field.newBuilder("opprinneligSendt", StandardSQLTypeName.TIMESTAMP)
                    .setDescription("Tidspunktet den opprinnelige søknaden ble sendt").build(),
                Field.newBuilder("endring", StandardSQLTypeName.STRING)
                    .setDescription("Hva slags endring det er, om hovedspørsmål eller underspørsmål er endret").build(),
                Field.newBuilder("tag", StandardSQLTypeName.STRING)
                    .setDescription("Tag på hovedspørsmålet som har endringen i seg").build(),
                Field.newBuilder("fom", StandardSQLTypeName.DATE).setDescription("Søknadens fra og med dato").build(),
                Field.newBuilder("tom", StandardSQLTypeName.DATE).setDescription("Søknadens til og med dato").build(),
                Field.newBuilder("hovedsvar", StandardSQLTypeName.STRING)
                    .setDescription("Det siste gjeldende hovedsvaret på spørsmålet. Kun av typen Ja/NEI").build(),
            )
        )

        createTable(
            tableName = andreInntektskilderTableName,
            schema = Schema.of(
                Field.newBuilder("sykepengesoknadId", StandardSQLTypeName.STRING)
                    .setDescription("Id på sykepengesøknad").build(),
                Field.newBuilder("soknadstype", StandardSQLTypeName.STRING)
                    .setDescription("Sykepengesøknad type").build(),
                Field.newBuilder("sendt", StandardSQLTypeName.TIMESTAMP)
                    .setDescription("Tidspunktet sykepengesøknaden ble sendt første gang").build(),
                Field.newBuilder("korriggerer", StandardSQLTypeName.STRING)
                    .setDescription("Id på sykepengesøknad som blir korrigert").build(),

                Field.newBuilder("andreArbeidsforholdSykmeldt", StandardSQLTypeName.BOOL)
                    .setDescription("Er du sykmeldt fra andre arbeidsforhold?").build(),

                Field.newBuilder("arbeidsforholdSykmeldt", StandardSQLTypeName.BOOL)
                    .setDescription("Er du sykmeldt fra andre arbeidsforhold?").build(),

                Field.newBuilder("selvstendigNaeringsdrivendeSykmeldt", StandardSQLTypeName.BOOL)
                    .setDescription("Er du sykmeldt fra selvstendig næringsdrivende?").build(),

                Field.newBuilder("dagmammaSykmeldt", StandardSQLTypeName.BOOL)
                    .setDescription("Er du sykmeldt fra dagmamma?").build(),

                Field.newBuilder("jordbrukFiskeReindriftSykmeldt", StandardSQLTypeName.BOOL)
                    .setDescription("Er du sykmeldt fra jordbruk / fiske / reindrift?").build(),

                Field.newBuilder("frilanserSykmeldt", StandardSQLTypeName.BOOL)
                    .setDescription("Er du sykmeldt fra frilanser?").build(),

                Field.newBuilder("frilanserSelvstendigSykmeldt", StandardSQLTypeName.BOOL)
                    .setDescription("Er du sykmeldt fra frilanser eller selvstendig næringsdrivende?").build(),

                Field.newBuilder("fosterhjemgodtgjorelseSykmeldt", StandardSQLTypeName.BOOL)
                    .setDescription("Er du sykmeldt med fosterhjemsgodtgjørelse?").build(),

                Field.newBuilder("omsorgslonnSykmeldt", StandardSQLTypeName.BOOL)
                    .setDescription("Er du sykmeldt med omsorgslønn fra kommunen?").build(),

                Field.newBuilder("annet", StandardSQLTypeName.BOOL)
                    .setDescription("Huket av for annet").build(),
            )
        )
    }

    fun createTable(tableName: String, schema: Schema) {

        val table = bigQuery.getTable(TableId.of(dataset, tableName))
        if (table != null && table.exists()) {
            log.info("Table $tableName eksisterer allerede")
            return
        }

        try {
            // Initialize client that will be used to send requests. This client only needs to be created
            // once, and can be reused for multiple requests.
            val tableId = TableId.of(dataset, tableName)
            val tableDefinition: TableDefinition = StandardTableDefinition.of(schema)
            val tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build()
            val create = bigQuery.create(tableInfo)
            log.info("Table $tableName created successfully: ${create.tableId.iamResourceName}")
        } catch (e: BigQueryException) {
            log.error("Table was not created.,", e)
            throw e
        }
    }
}

const val korrigerteSporsmalTableName = "korrigerte_sporsmal"
const val andreInntektskilderTableName = "andre_inntektskilder"
