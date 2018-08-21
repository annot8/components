package io.annot8.components.files.processors;


import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.components.AbstractComponent;
import io.annot8.core.capabilities.CreatesContent;
import io.annot8.core.capabilities.ProcessesContent;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Item;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;

@ProcessesContent(FileContent.class)
@CreatesContent(Text.class)
public class TxtFileExtractor extends AbstractComponent implements Processor {

  @Override
  public ProcessorResponse process(Item item) {

    item.getContents(FileContent.class)
        .filter(f -> f.getData().getName().endsWith(".txt"))
        .forEach(f -> {
          try {
            File file = f.getData();
            String data = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
            item.create(Text.class)
                .withName("text")
                .withData(data)
                .save();

            // If we processed it ... lets remove it from our item
            // so it doesn't get reprocessed
            item.removeContent(f.getName());

          } catch (Exception e) {
            e.printStackTrace();
          }


        });

    // Always carry on it
    return ProcessorResponse.ok();
  }

}