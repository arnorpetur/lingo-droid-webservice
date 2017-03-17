import groovy.sql.GroovyRowResult
import groovy.util.logging.Slf4j
import rx.Observable

import javax.inject.Inject

@Slf4j
class UserService {

    private final UserDbCommands userDbCommands

    @Inject
    UserService(UserDbCommands userDbCommands) {
        this.userDbCommands = userDbCommands
    }

    void createTable() {
        log.info("Creating database tables")
        userDbCommands.createTables()
    }

    Observable<User> all() {
        userDbCommands.getAll().map { row ->
            new User(
                    row.icelandic,
                    row.english,
                    row.difficulty
            )
        }
    }

    Observable<String> insert(String icelandic, String english, int difficulty) {
        userDbCommands.insert(icelandic, english, difficulty).
                map {
                    icelandic
                }
    }

    Observable<User> find(String icelandic) {
        userDbCommands.find(icelandic).map { GroovyRowResult dbRow ->
            return new User(
                    icelandic,
                    dbRow.english,
                    dbRow.difficulty
            )
        }
    }

    /*Observable<Void> update(String isbn, long quantity, BigDecimal price) {
        bookDbCommands.update(isbn, quantity, price)
    }*/

    Observable<Void> delete(String icelandic) {
        userDbCommands.delete(icelandic)
    }
}