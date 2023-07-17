package dev.tricked.papertimeseries

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime



//object Users : Table() {
//    val id: Column<String> = varchar("id", 10)
//    val name: Column<String> = varchar("name", length = 50)
//    val cityId: Column<Int?> = (integer("city_id") references Cities.id).nullable()
//
//    override val primaryKey = PrimaryKey(id, name = "PK_User_ID") // name is optional here
//}
//
//object Cities : Table() {
//    val id: Column<Int> = integer("id").autoIncrement()
//    val name: Column<String> = varchar("name", 50)
//
//    override val primaryKey = PrimaryKey(id, name = "PK_Cities_ID")
//}