import ratpack.groovy.handling.GroovyChainAction

import javax.inject.Inject

import static ratpack.jackson.Jackson.json
import static ratpack.jackson.Jackson.jsonNode
import static ratpack.rx.RxRatpack.observe

class WordEndpoint extends GroovyChainAction {

  private final WordService wordService

  @Inject
  WordEndpoint(WordService wordService) {
    this.wordService = wordService
  }

  @Override
  void execute() throws Exception {
    post("insert") {
      parse(jsonNode()).
              observe().
              flatMap { input ->
                wordService.insert(
                        input.get("icelandic").asText(),
                        input.get("english").asText(),
                        input.get("difficulty").asInt()
                )
              }.
              single().
              flatMap { icelandic ->
                wordService.find(icelandic)
              }.
              single().
              subscribe { Word createdWord ->
                render json(createdWord)
              }
    }

    post("delete"){
      parse(jsonNode()).
              observe().
              flatMap { input ->
                wordService.delete(input.get("icelandic").asText())
              }.subscribe {
                response.send()
              }
    }



    all {
      byMethod {
        get {
          wordService.all().
                  toList().
                  subscribe { List<Word> word ->
                    render json(word)
                  }
        }
      }
    }

  }
}