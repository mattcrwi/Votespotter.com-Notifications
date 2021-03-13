package com.matthew.williams.covidvaccinenotificaiton

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class FeatureCollection constructor(
    var type: String?,
    var features: Array<Feature>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FeatureCollection

        if (type != other.type) return false
        if (!features.contentEquals(other.features)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type?.hashCode() ?: 0
        result = 31 * result + features.contentHashCode()
        return result
    }
}

data class Feature constructor(
    var type: String?,
    var geometry: Geometry?,
    var properties: Properties?
)

data class Geometry constructor(
    var type: String?,
    var coordinates: ArrayList<Double>?
)

data class Properties constructor(
    var id: Int,
    var url: String?,
    var city: String?,
    var name: String?,
    var state: String?,
    var addres: String?,
    var provider: String?,
    @JsonProperty("time_zone") var timeZone: String?,
    @JsonProperty("postal_code") var postalCode: String?,
    //var appointments: Array<String>,
    @JsonProperty("provider_brand") var providerBrand: String?,
    @JsonProperty("carries_vaccine") var carriesVaccine: Boolean?,
    @JsonProperty("provider_brand_name") var providerBrandName: String?,
    @JsonProperty("provider_location_id") var providerLocationId: String?,
    @JsonProperty("appointments_available") var appointmentsAvailable: Boolean?,
    @JsonProperty("appointments_last_fetched") var appointmentsLastFetched: ZonedDateTime?,
)