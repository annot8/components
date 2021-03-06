module io.annot8.components.files {
  requires transitive io.annot8.core;
  requires io.annot8.common.data;
  requires io.annot8.conventions;
  requires transitive io.annot8.components.base;

  exports io.annot8.components.files.sources;
  exports io.annot8.components.files.processors;

  requires slf4j.api;
  requires apache.mime4j.dom;
  requires com.google.common;
  requires apache.mime4j.core;
}
