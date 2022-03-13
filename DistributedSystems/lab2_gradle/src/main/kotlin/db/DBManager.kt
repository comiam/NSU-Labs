package db

import db.dao.NodeServiceFactory
import db.datasource.IConnectionManager
import db.transaction.TransactionManager
import model.NodeEntity
import models.Node
import parser.XmlParser
import utils.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.measureTimeMillis

object DBManager {
    const val DEFAULT_PROPERTIES_PATH = "application.yaml"
    const val DEFAULT_INIT_SCRIPT_PATH = "init.sql"

    private val log by LoggerFactory()
    private lateinit var transactionManager: TransactionManager

    @PublishedApi
    internal fun runInsertionTest(
        inputFileName: String,
        connectionManagerCreator: () -> IConnectionManager,
        strategy: NodeServiceFactory.Strategy
    ) {
        val connectionManager = connectionManagerCreator()
        transactionManager = TransactionManager(
            connectionManager
        )

        initDB()

        log.info("Exec insertion with stategy ${strategy.name} by ${connectionManager.javaClass.name}")
        try {
            measureTimeMillis {
                NodeServiceFactory.createService(
                    strategy,
                    transactionManager
                ).use { service ->
                    var itCount = 0
                    XmlParser.process<Node>(inputFileName) {
                        service.save(
                            NodeEntity.toDbNode(it)
                        )
                        itCount++
                        if (itCount % 10000 == 0) {
                            log.info("Saved $itCount nodes")
                        }
                    }
                }
                log.info("End processing file $inputFileName using strategy $strategy")
            }.let {
                log.info("Elapsed: %.3f sec".format(it / 1000F))
            }
        } catch (exc: Throwable) {
            log.error(exc.localizedMessage)
            connectionManager.close()
            return
        }

        log.info("closing all")
        connectionManager.close()
    }

    private fun initDB() {
        val script = Files.readString(
            Path.of(
                this.javaClass
                    .classLoader
                    .getResource(DEFAULT_INIT_SCRIPT_PATH)?.toURI()
            )
        )

        log.info("Initializing DB using script $DEFAULT_INIT_SCRIPT_PATH")
        transactionManager.runInTransaction {
            createStatement().use { statement ->
                statement.execute(script)
            }
        }
        log.info("DB Initialised successfully")
    }
}