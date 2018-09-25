/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

import io.annot8.common.data.bounds.NoBounds;
import io.annot8.common.data.content.FileContent;
import io.annot8.components.base.components.AbstractComponent;
import io.annot8.core.capabilities.ProcessesContent;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.IncompleteException;
import io.annot8.core.stores.AnnotationStore;

@ProcessesContent(FileContent.class)
public class FileMetadataExtractor extends AbstractComponent implements Processor {

  public static final String FILE_METADATA = "FILE_METADATA";

  @Override
  public ProcessorResponse process(Item item) {
    boolean withoutErrors =
        item.getContents(FileContent.class)
            .map(this::extractMetadata)
            .reduce(true, (a, b) -> a && b);

    if (!withoutErrors) {
      return ProcessorResponse.itemError();
    }

    return ProcessorResponse.ok();
  }

  private boolean extractMetadata(FileContent fileContent) {
    File file = fileContent.getData();

    if (!file.exists()) {
      return false;
    }

    BasicFileAttributes attr;
    boolean isHidden;
    boolean isRegular;
    boolean isDir;
    boolean isSym;
    String owner;
    try {
      isHidden = Files.isHidden(file.toPath());
      isRegular = Files.isRegularFile(file.toPath());
      isDir = Files.isDirectory(file.toPath());
      isSym = Files.isSymbolicLink(file.toPath());
      owner = Files.getOwner(file.toPath()).getName();
    } catch (IOException e) {
      log().error("Failed to retrieve file metadata", e);
      return false;
    }

    try {
      attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
    } catch (IOException e) {
      log().error("Failed to process file attributes", e);
      return false;
    }

    if (attr != null) {
      createMetadataAnnotation(
          fileContent, FileMetadata.DATE_CREATED, attr.creationTime().toMillis());
      createMetadataAnnotation(
          fileContent, FileMetadata.LAST_MODIFIED, attr.lastModifiedTime().toMillis());
      createMetadataAnnotation(
          fileContent, FileMetadata.LAST_ACCESS_DATE, attr.lastAccessTime().toMillis());
      createMetadataAnnotation(fileContent, FileMetadata.FILE_SIZE, attr.size());
    }
    createMetadataAnnotation(fileContent, FileMetadata.PATH, file.getAbsolutePath());
    createMetadataAnnotation(fileContent, FileMetadata.HIDDEN, isHidden);
    createMetadataAnnotation(fileContent, FileMetadata.REGULAR, isRegular);
    createMetadataAnnotation(fileContent, FileMetadata.DIRECTORY, isDir);
    createMetadataAnnotation(fileContent, FileMetadata.SYM_LINK, isSym);
    createMetadataAnnotation(fileContent, FileMetadata.OWNER, owner);

    String extension = getFileExtension(file);
    if (extension != null) {
      createMetadataAnnotation(fileContent, FileMetadata.EXTENSION, extension);
    }
    return true;
  }

  private String getFileExtension(File file) {
    int index = file.getAbsolutePath().lastIndexOf('.');
    if (index > 0 && index != file.getAbsolutePath().length()) {
      return file.getAbsolutePath().substring(index + 1);
    }
    return null;
  }

  private void createMetadataAnnotation(FileContent content, String key, Object value) {
    AnnotationStore annotations = content.getAnnotations();
    try {
      annotations
          .create()
          .withType(FILE_METADATA)
          .withBounds(NoBounds.getInstance())
          .withProperty(key, value)
          .save();
    } catch (IncompleteException e) {
      log().error("Failed to create file metadata annotation", e);
    }
  }
}