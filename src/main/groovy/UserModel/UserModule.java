import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class UserModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(UserService.class).in(Scopes.SINGLETON);
    bind(UserDbCommands.class).in(Scopes.SINGLETON);
    bind(UserEndpoint.class).in(Scopes.SINGLETON);
  }

}