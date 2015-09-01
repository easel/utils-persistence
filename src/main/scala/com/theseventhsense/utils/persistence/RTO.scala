package com.theseventhsense.utils.persistence

/**
 * "Resource Transfer Object" -- used to represent the data schema for a
 * resource. Vs a Domain object and a DTO.
 */

trait RTO extends Product {
  val resourceUri: Option[RTO.ResourceURI]
}

object RTO {
  type ResourceURI = String
  type ResourceId = Long
  type ResourceName = String
}

