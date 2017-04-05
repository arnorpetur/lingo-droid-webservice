import ratpack.groovy.handling.GroovyChainAction

import javax.inject.Inject

import static ratpack.jackson.Jackson.json
import static ratpack.jackson.Jackson.jsonNode

class UserEndpoint extends GroovyChainAction {

  private final UserService userService

  @Inject
  UserEndpoint(UserService userService) {
    this.userService = userService
  }

  @Override
  void execute() throws Exception {
    post("insert") {
      parse(jsonNode()).
              observe().
              flatMap { input ->
                userService.insert(
                        input.get("id").asText(),
                        input.get("userName").asText(),
                        input.get("score").asInt()
                )
              }.
              single().
              flatMap { id ->
                userService.find(id)
              }.
              single().
              subscribe { User createdUser ->
                render json(createdUser)
              }
    }

    post("delete") {
      parse(jsonNode()).
              observe().
              flatMap { input ->
                userService.delete(input.get("id").asText())
              }.subscribe {
                response.send()
              }
    }

    post("update") {
      parse(jsonNode()).
              observe().
              flatMap { input ->
                userService.update(
                    input.get("id").asText(),
                    input.get("userName").asText(),
                    input.get("score").asInt()
                  )
              }.subscribe {
                response.send()
              }
    }

    path(":isbn") {
      def isbn = pathTokens["isbn"]
      all {
        byMethod {
          get {
            userService.all(isbn).
                    toList().
                    subscribe { List<Word> user ->
                      render json(user)
                    }
          }
        }
      }
    }
  }
}