package io.annot8.components.files.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Text;
import io.annot8.common.implementations.stores.AnnotationStoreFactory;
import io.annot8.components.monitor.resources.Logging;
import io.annot8.core.components.Processor;
import io.annot8.core.components.Resource;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.data.ItemFactory;
import io.annot8.defaultimpl.content.SimpleFile;
import io.annot8.defaultimpl.content.SimpleInputStream;
import io.annot8.defaultimpl.content.SimpleText;
import io.annot8.defaultimpl.context.SimpleContext;
import io.annot8.defaultimpl.data.SimpleItem;
import io.annot8.defaultimpl.factories.SimpleContentBuilderFactoryRegistry;
import io.annot8.defaultimpl.factories.SimpleItemFactory;
import io.annot8.defaultimpl.stores.SimpleAnnotationStore;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

public class EmlFileExtractorTest {

  @Test
  public void test() throws Exception{

    List<Item> newItems = new ArrayList<>();
    Consumer<Item> consumer = newItems::add;

    try(
      Processor p = new EmlFileExtractor()
    ) {
      AnnotationStoreFactory factory = SimpleAnnotationStore.factory();
      SimpleContentBuilderFactoryRegistry contentBuilderFactoryRegistry = new SimpleContentBuilderFactoryRegistry();
      contentBuilderFactoryRegistry.register(Text.class, new SimpleText.BuilderFactory(factory));
      contentBuilderFactoryRegistry.register(InputStreamContent.class, new SimpleInputStream.BuilderFactory(factory));
      contentBuilderFactoryRegistry.register(FileContent.class, new SimpleFile.BuilderFactory(factory));

      ItemFactory itemFactory = new SimpleItemFactory(contentBuilderFactoryRegistry, consumer);

      Logging logging = Logging.useLoggerFactory();
      Map<String, Resource> resources = new HashMap<>();
      resources.put("logging", logging);

      Context context = new SimpleContext( resources);
      p.configure(context);

      Item item = new SimpleItem(itemFactory, contentBuilderFactoryRegistry);

      URL resource = EmlFileExtractorTest.class.getResource("test_sample_message.eml");   //Based on https://www.phpclasses.org/browse/file/14672.html
      File f = Paths.get(resource.toURI()).toFile();

      item.create(FileContent.class)
          .withName("test_sample_message.eml")
          .withData(f)
          .save();

      p.process(item);

      assertEquals("mlemos <mlemos@acm.org>", item.getProperties().get("From").get());
      assertEquals("Manuel Lemos <mlemos@linux.local>", item.getProperties().get("To").get());
      assertEquals("http://www.phpclasses.org/mimemessage $Revision: 1.63 $ (mail)", item.getProperties().get("X-Mailer").get());
      assertEquals("Sat, 30 Apr 2005 19:28:29 -0300", item.getProperties().get("Date").get());
      assertEquals(Arrays.asList("Original file from https://www.phpclasses.org/browse/file/14672.html", "Modified by James Baker"), item.getProperties().get("Comment").get());

      Text text1 = (Text) item.getContentByName("body-1-1-1").findFirst().get();
      assertNotNull(text1);
      assertEquals("text/plain; charset=ISO-8859-1", text1.getProperties().get("Content-Type").get());
      assertTrue(text1.getData().contains("Please use an HTML capable mail program"));

      Text text2 = (Text) item.getContentByName("body-1-1-2").findFirst().get();
      assertNotNull(text2);
      assertEquals("text/html; charset=ISO-8859-1", text2.getProperties().get("Content-Type").get());
      assertTrue(text2.getData().contains("Testing Manuel Lemos' MIME E-mail composing and sending PHP class: HTML message"));

      assertEquals(3, newItems.size());

      Item logoItem = newItems.get(0);
      assertTrue(logoItem.hasContentOfName("logo.gif"));

      Item backgroundItem = newItems.get(1);
      assertTrue(backgroundItem.hasContentOfName("background.gif"));

      Item attachmentItem = newItems.get(2);
      InputStreamContent inputStreamContent = (InputStreamContent) attachmentItem.getContentByName("attachment.txt").findFirst().get();
      assertNotNull(inputStreamContent);
      String content = CharStreams
          .toString(new InputStreamReader(inputStreamContent.getData(), Charsets.ISO_8859_1));
      assertEquals("This is just a plain text attachment file named attachment.txt .", content);
      assertEquals("This is just a plain text attachment file named attachment.txt .", content);
    }
  }
}
