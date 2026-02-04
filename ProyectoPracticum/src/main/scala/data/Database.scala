package data

import cats.effect.*
import doobie.*
import doobie.hikari.*
import doobie.implicits.*
import scala.concurrent.ExecutionContext

object Database {

  // Configuración de conexión optimizada para Scala 3
  def createTransactor(
                        host: String = "localhost",
                        port: Int = 3306,
                        database: String = "peliculas_db",        
                        user: String = "root",                    
                        password: String = "Ju@nD@2006",          
                        poolSize: Int = 10
                      ): Resource[IO, HikariTransactor[IO]] = {

     
    val url = s"jdbc:mysql://$host:$port/$database?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"

    for {
      xa <- HikariTransactor.newHikariTransactor[IO](
        driverClassName = "com.mysql.cj.jdbc.Driver",
        url = url,
        user = user,
        pass = password,
        connectEC = ExecutionContext.global,
        logHandler = None
      )
      _ <- Resource.eval {
        xa.configure { ds =>
          IO {
            ds.setMaximumPoolSize(poolSize)
            ds.setMinimumIdle(2)
            ds.setConnectionTimeout(30000)
            ds.setIdleTimeout(600000)
            ds.setMaxLifetime(1800000)
            // Optimizaciones para MySQL
            ds.addDataSourceProperty("cachePrepStmts", "true")
            ds.addDataSourceProperty("prepStmtCacheSize", "250")
            ds.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            ds.addDataSourceProperty("useServerPrepStmts", "true")
            ds.addDataSourceProperty("rewriteBatchedStatements", "true")
            ds
          }
        }
      }
    } yield xa
  }

  // Transactor por defecto
  val transactor: Resource[IO, HikariTransactor[IO]] = createTransactor()

  // Helper para verificar conexión
  def checkConnection(using xa: Transactor[IO]): IO[Boolean] =
    sql"SELECT 1".query[Int].unique.transact(xa).map(_ == 1).handleError(_ => false)
}