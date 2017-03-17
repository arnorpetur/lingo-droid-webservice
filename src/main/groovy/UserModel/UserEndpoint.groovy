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
              flatMap { userName ->
                userService.find(userName)
              }.
              single().
              subscribe { User createdUser ->
                render json(createdUser)
              }
    }

    all {
      byMethod {
        get {
          userService.all().
                  toList().
                  subscribe { List<Word> user ->
                    render json(user)
                  }
        }
      }
    }

  }
}