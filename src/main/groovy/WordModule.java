import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class WordModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(WordService.class).in(Scopes.SINGLETON);
    bind(WordDbCommands.class).in(Scopes.SINGLETON);
    bind(WordEndpoint.class).in(Scopes.SINGLETON);
  }

}