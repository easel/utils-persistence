package com.theseventhsense.utils.persistence.db

import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

@javax.inject.Singleton
class SlickContext @Inject() (
    dbConfigProvider: DatabaseConfigProvider
) {
  val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]
  val driver: JdbcProfile = dbConfig.driver
  val db: JdbcBackend#DatabaseDef = dbConfig.db
}
