import com.zaxxer.hikari.HikariConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.groovy.sql.SqlModule
import ratpack.groovy.template.MarkupTemplateModule
import ratpack.handling.RequestLogger
import ratpack.hikari.HikariModule
import ratpack.hystrix.HystrixModule
import ratpack.rx.RxRatpack
import ratpack.service.Service
import ratpack.service.StartEvent

import static ratpack.groovy.Groovy.groovyMarkupTemplate
import static ratpack.groovy.Groovy.ratpack

final Logger logger = LoggerFactory.getLogger(Ratpack.class);

ratpack {
  bindings {
    module MarkupTemplateModule
    module HikariModule, { HikariConfig c ->
      c.addDataSourceProperty("url", System.getenv("JDBC_DATABASE_URL"))
      c.setDataSourceClassName("org.postgresql.ds.PGPoolingDataSource")
    }
    module SqlModule
    module WordModule
    module UserModule
    module new HystrixModule().sse()

    bindInstance Service, new Service() {
      @Override
      void onStart(StartEvent event) throws Exception {
        RxRatpack.initialize()
        event.registry.get(WordService).createTable()
        event.registry.get(UserService).createTable()

      }
    }
  }

  handlers { WordService wordService, UserService userService ->
    all RequestLogger.ncsa(logger)

    get {
      render groovyMarkupTemplate("index.gtpl", title: "My Ratpack App")
    }

    prefix("words") {
      all chain(registry.get(WordEndpoint))
    }

    files { dir "public" }

    prefix("users") {
      all chain(registry.get(UserEndpoint))
    }

  }

}