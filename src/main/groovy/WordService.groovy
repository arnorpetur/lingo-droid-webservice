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
        wordDbCommands.insert(icelandic, english, difficulty)
    }

    Observable<Word> find(String english) {
        wordDbCommands.find(icelandic).map { GroovyRowResult dbRow ->
            return new Word(
                    dbrow.icelandic,
                    english,
                    dbRow.difficulty
            )
        }
    }

    /*Observable<Void> update(String isbn, long quantity, BigDecimal price) {
        bookDbCommands.update(isbn, quantity, price)
    }*/

    Observable<Void> delete(String icelandic) {
        wordDbCommands.delete(icelandic)
    }
}