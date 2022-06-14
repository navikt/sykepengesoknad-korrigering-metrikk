package no.nav.helse.flex.bigquery

import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.BigQueryOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BigQueryServiceConfig {

    @Bean
    fun bigQuery(@Value("\${GCP_TEAM_PROJECT_ID}") projectId: String): BigQuery {
        return BigQueryOptions.newBuilder().setProjectId(projectId).build().service
    }
}

const val dataset = "korrigering_metrikk"
