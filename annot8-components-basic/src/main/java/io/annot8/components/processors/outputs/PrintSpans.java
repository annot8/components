package io.annot8.components.processors.outputs;

import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextAnnotator;
import io.annot8.core.data.Item;
import java.util.Optional;

public class PrintSpans extends AbstractTextAnnotator {


  @Override
  protected void process(final Item item, final Text content) {

    content.getAnnotations().getByBounds(SpanBounds.class)
        .forEach(a -> {
          final SpanBounds bounds = a.getBounds(SpanBounds.class).get();
          final Optional<String> value = content.getText(a);

          System.out.println(
              String.format("Annotation %s: %s", bounds.toString(), value.orElse("UNKNOWN"))
          );

        });
  }


}