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
                    row.id,
                    row.userName,
                    row.score
            )
        }
    }

    Observable<String> insert(String id, String userName, int score) {
        userDbCommands.insert(id, userName, score).
                map {
                    userName
                }
    }

    Observable<User> find(String id) {
        userDbCommands.find(id).map { GroovyRowResult dbRow ->
            return new User(
                    id,
                    dbRow.userName,
                    dbRow.score
            )
        }
    }

    Observable<Void> update(String id, String userName, int score) {
        userDbCommands.update(id, userName, score)
    }

    Observable<Void> delete(String userName) {
        userDbCommands.delete(userName)
    }
}