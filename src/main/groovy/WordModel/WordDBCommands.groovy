import com.google.inject.Inject
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ratpack.exec.Blocking

import static ratpack.rx.RxRatpack.observe
import static ratpack.rx.RxRatpack.observeEach

class WordDbCommands {

    private final Sql sql
    private static final HystrixCommandGroupKey hystrixCommandGroupKey = HystrixCommandGroupKey.Factory.asKey("sql-dingodb")

    @Inject
    public WordDbCommands(Sql sql) {
        this.sql = sql
    }

    void createTables() {
        sql.execute("DROP TABLE IF EXISTS dictionary")
        sql.execute("CREATE TABLE dictionary ("+
            "icelandic varchar(25), "+
            "english varchar(25), "+
            "difficulty int, "+
            "CONSTRAINT dictionary_icelandic UNIQUE (icelandic)"+
            ")")
    }

    rx.Observable<GroovyRowResult> getAll() {
        return new HystrixObservableCommand<GroovyRowResult>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("getAll"))) {

            @Override
            protected rx.Observable<GroovyRowResult> construct() {
                observeEach(Blocking.get {
                    sql.rows("SELECT icelandic, english, difficulty FROM dictionary ORDER BY difficulty")
                })
            }

            @Override
            protected String getCacheKey() {
                return "db-dingodb-all"
            }
        }.toObservable()
    }

    rx.Observable<String> insert(final String icelandic, final String english, final int difficulty) {
        return new HystrixObservableCommand<String>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("insert"))) {

            @Override
            protected rx.Observable<List<Object>> construct() {
                observe(Blocking.get {
                    sql.executeInsert("INSERT INTO dictionary (icelandic, english, difficulty) VALUES ($icelandic, $english, $difficulty)")
                })
            }
        }.toObservable()
    }

    rx.Observable<GroovyRowResult> find(final String icelandic) {
        return new HystrixObservableCommand<GroovyRowResult>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("find"))) {

            @Override
            protected rx.Observable<GroovyRowResult> construct() {
                observe(Blocking.get {
                    sql.firstRow("SELECT english, difficulty FROM dictionary WHERE icelandic = $icelandic")
                })
            }

            @Override
            protected String getCacheKey() {
                return "db-dingodb-find-$icelandic"
            }
        }.toObservable()
    }

    /*rx.Observable<Void> update(final String isbn, final long quantity, final BigDecimal price) {
        return new HystrixObservableCommand<Void>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("update"))) {

            @Override
            protected rx.Observable<Integer> construct() {
                observe(Blocking.get {
                    sql.executeUpdate("update books set quantity = $quantity, price = $price where isbn = $isbn")
                })
            }
        }.toObservable()
    }*/

    rx.Observable<Void> delete(final String icelandic) {
        return new HystrixObservableCommand<Void>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("delete"))) {

            @Override
            protected rx.Observable<Integer> construct() {
                observe(Blocking.get {
                    sql.executeUpdate("DELETE FROM dictionary WHERE icelandic = $icelandic")
                })
            }
        }.toObservable()
    }

}