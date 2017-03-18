import groovy.sql.GroovyRowResult
import groovy.util.logging.Slf4j
import rx.Observable

import javax.inject.Inject

@Slf4j
class WordService {

    private final WordDbCommands wordDbCommands

    @Inject
    WordService(WordDbCommands wordDbCommands) {
        this.wordDbCommands = wordDbCommands
    }

    void createTable() {
        log.info("Creating database tables")
        wordDbCommands.createTables()
    }

    Observable<Word> all() {
        wordDbCommands.getAll().map { row ->
            new Word(
                    row.icelandic,
                    row.english,
                    row.difficulty
            )
        }
    }

    Observable<String> insert(String icelandic, String english, int difficulty) {
        wordDbCommands.insert(icelandic, english, difficulty).
                map {
                    icelandic
                }
    }

    Observable<Word> find(String icelandic) {
        wordDbCommands.find(icelandic).map { GroovyRowResult dbRow ->
            return new Word(
                    icelandic,
                    dbRow.english,
                    dbRow.difficulty
            )
        }
    }

    Observable<Void> delete(String icelandic) {
        wordDbCommands.delete(icelandic)
    }
}