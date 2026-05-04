package io.github.mahmoud.ktorscope.persistence

/**
 * Created by Mahmoud kamal El-Din on 04/05/2026
 */
private const val DEFAULT_DATABASE_NAME = "network_inspector.db"
private const val DEFAULT_BODY_DIRECTORY_NAME = "network_inspector_bodies"

expect class ScopPersistenceFactory {

     fun create(
        databaseName: String = DEFAULT_DATABASE_NAME ,
        bodyDirectoryName: String = DEFAULT_BODY_DIRECTORY_NAME,
    ): KtorScopePersistence
}
