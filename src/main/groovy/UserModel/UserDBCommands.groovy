import com.google.inject.Inject
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ratpack.exec.Blocking

import static ratpack.rx.RxRatpack.observe
import static ratpack.rx.RxRatpack.observeEach

class UserDbCommands {

    private final Sql sql
    private static final HystrixCommandGroupKey hystrixCommandGroupKey = HystrixCommandGroupKey.Factory.asKey("sql-dingodb")

    @Inject
    public UserDbCommands(Sql sql) {
        this.sql = sql
    }

    void createTables() {
        /*sql.execute("DROP TABLE IF EXISTS userinfo")
        sql.execute("CREATE TABLE userinfo ("+
            "id varchar(48), "+
            "username varchar(25), "+
            "score int, "+
            "CONSTRAINT user_username UNIQUE (username)"+
            ")")*/
    }

    rx.Observable<GroovyRowResult> getAll(String id) {
        return new HystrixObservableCommand<GroovyRowResult>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("getAll"))) {

            @Override
            protected rx.Observable<GroovyRowResult> construct() {
                observeEach(Blocking.get {
                    sql.rows("(SELECT * FROM userinfo ORDER BY score DESC LIMIT 10) "+
                        "UNION (SELECT * FROM userinfo WHERE id='$id') ORDER BY score;")
                })
            }

            @Override
            protected String getCacheKey() {
                return "db-dingodb-all"
            }
        }.toObservable()
    }

    rx.Observable<String> insert(final String id, final String userName, final int score) {
        return new HystrixObservableCommand<String>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("insert"))) {

            @Override
            protected rx.Observable<List<Object>> construct() {
                observe(Blocking.get {
                    sql.executeInsert("INSERT INTO userinfo (id, username, score) VALUES ($id, $userName, $score)")
                })
            }
        }.toObservable()
    }

    rx.Observable<GroovyRowResult> find(final String id) {
        return new HystrixObservableCommand<GroovyRowResult>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("find"))) {

            @Override
            protected rx.Observable<GroovyRowResult> construct() {
                observe(Blocking.get {
                    sql.firstRow("SELECT username, score FROM userinfo WHERE id = $id")
                })
            }

            @Override
            protected String getCacheKey() {
                return "db-dingodb-find-$id"
            }
        }.toObservable()
    }

    rx.Observable<Void> update(final String id, final String userName, int score) {
        return new HystrixObservableCommand<Void>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("update"))) {

            @Override
            protected rx.Observable<Integer> construct() {
                observe(Blocking.get {
                    sql.executeUpdate("UPDATE userinfo SET score = $score WHERE id = $id")
                })
            }
        }.toObservable()
    }

    rx.Observable<Void> delete(final String id) {
        return new HystrixObservableCommand<Void>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("delete"))) {

            @Override
            protected rx.Observable<Integer> construct() {
                observe(Blocking.get {
                    sql.executeUpdate("DELETE FROM userinfo WHERE id = $id")
                })
            }
        }.toObservable()
    }

}