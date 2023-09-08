/*
 * Copyright 2023 Datastrato.
 * This software is licensed under the Apache License version 2.
 */
package com.datastrato.graviton.dto.requests;

import com.datastrato.graviton.json.JsonUtils;
import com.datastrato.graviton.rel.TableChange;
import com.datastrato.graviton.rest.RESTRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import io.substrait.type.Type;
import java.util.Arrays;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
  @JsonSubTypes.Type(value = TableUpdateRequest.RenameTableRequest.class, name = "rename"),
  @JsonSubTypes.Type(
      value = TableUpdateRequest.UpdateTableCommentRequest.class,
      name = "updateComment"),
  @JsonSubTypes.Type(
      value = TableUpdateRequest.SetTablePropertyRequest.class,
      name = "setProperty"),
  @JsonSubTypes.Type(
      value = TableUpdateRequest.RemoveTablePropertyRequest.class,
      name = "removeProperty"),
  @JsonSubTypes.Type(value = TableUpdateRequest.AddTableColumnRequest.class, name = "addColumn"),
  @JsonSubTypes.Type(
      value = TableUpdateRequest.RenameTableColumnRequest.class,
      name = "renameColumn"),
  @JsonSubTypes.Type(
      value = TableUpdateRequest.UpdateTableColumnTypeRequest.class,
      name = "updateColumnType"),
  @JsonSubTypes.Type(
      value = TableUpdateRequest.UpdateTableColumnCommentRequest.class,
      name = "updateColumnComment"),
  @JsonSubTypes.Type(
      value = TableUpdateRequest.UpdateTableColumnPositionRequest.class,
      name = "updateColumnPosition"),
  @JsonSubTypes.Type(
      value = TableUpdateRequest.DeleteTableColumnRequest.class,
      name = "deleteColumn")
})
public interface TableUpdateRequest extends RESTRequest {

  TableChange tableChange();

  @EqualsAndHashCode
  @ToString
  class RenameTableRequest implements TableUpdateRequest {

    @Getter
    @JsonProperty("newName")
    private final String newName;

    public RenameTableRequest(String newName) {
      this.newName = newName;
    }

    public RenameTableRequest() {
      this(null);
    }

    @Override
    public void validate() throws IllegalArgumentException {
      Preconditions.checkArgument(
          StringUtils.isNotBlank(newName), "\"newName\" field is required and cannot be empty");
    }

    @Override
    public TableChange tableChange() {
      return TableChange.rename(newName);
    }
  }

  @EqualsAndHashCode
  @ToString
  class UpdateTableCommentRequest implements TableUpdateRequest {

    @Getter
    @JsonProperty("newComment")
    private final String newComment;

    public UpdateTableCommentRequest(String newComment) {
      this.newComment = newComment;
    }

    public UpdateTableCommentRequest() {
      this(null);
    }

    @Override
    public void validate() throws IllegalArgumentException {
      Preconditions.checkArgument(
          StringUtils.isNotBlank(newComment),
          "\"newComment\" field is required and cannot be empty");
    }

    @Override
    public TableChange tableChange() {
      return TableChange.updateComment(newComment);
    }
  }

  @EqualsAndHashCode
  @ToString
  class SetTablePropertyRequest implements TableUpdateRequest {

    @Getter
    @JsonProperty("property")
    private final String property;

    @Getter
    @JsonProperty("value")
    private final String value;

    public SetTablePropertyRequest(String property, String value) {
      this.property = property;
      this.value = value;
    }

    public SetTablePropertyRequest() {
      this(null, null);
    }

    @Override
    public void validate() throws IllegalArgumentException {
      Preconditions.checkArgument(
          StringUtils.isNotBlank(property), "\"property\" field is required and cannot be empty");
      Preconditions.checkArgument(
          StringUtils.isNotBlank(value), "\"value\" field is required and cannot be empty");
    }

    @Override
    public TableChange tableChange() {
      return TableChange.setProperty(property, value);
    }
  }

  @EqualsAndHashCode
  @ToString
  class RemoveTablePropertyRequest implements TableUpdateRequest {

    @Getter
    @JsonProperty("property")
    private final String property;

    public RemoveTablePropertyRequest(String property) {
      this.property = property;
    }

    public RemoveTablePropertyRequest() {
      this(null);
    }

    @Override
    public void validate() throws IllegalArgumentException {
      Preconditions.checkArgument(
          StringUtils.isNotBlank(property), "\"property\" field is required and cannot be empty");
    }

    @Override
    public TableChange tableChange() {
      return TableChange.removeProperty(property);
    }
  }

  @EqualsAndHashCode
  @ToString
  class AddTableColumnRequest implements TableUpdateRequest {

    @Getter
    @JsonProperty("fieldName")
    private final String[] fieldName;

    @Getter
    @JsonProperty("type")
    @JsonSerialize(using = JsonUtils.TypeSerializer.class)
    @JsonDeserialize(using = JsonUtils.TypeDeserializer.class)
    private final Type dataType;

    @Getter
    @JsonProperty("comment")
    @Nullable
    private final String comment;

    @Getter
    @JsonProperty("position")
    @JsonSerialize(using = JsonUtils.ColumnPositionSerializer.class)
    @JsonDeserialize(using = JsonUtils.ColumnPositionDeserializer.class)
    @Nullable
    private final TableChange.ColumnPosition position;

    public AddTableColumnRequest(
        String[] fieldName, Type dataType, String comment, TableChange.ColumnPosition position) {
      this.fieldName = fieldName;
      this.dataType = dataType;
      this.comment = comment;
      this.position = position;
    }

    public AddTableColumnRequest() {
      this(null, null, null, null);
    }

    @Override
    public void validate() throws IllegalArgumentException {
      Preconditions.checkArgument(
          fieldName != null
              && fieldName.length > 0
              && Arrays.stream(fieldName).allMatch(StringUtils::isNotBlank),
          "\"fieldName\" field is required and cannot be empty");
      Preconditions.checkArgument(
          dataType != null, "\"type\" field is required and cannot be empty");
    }

    @Override
    public TableChange tableChange() {
      return TableChange.addColumn(fieldName, dataType, comment, position);
    }
  }

  @EqualsAndHashCode
  @ToString
  class RenameTableColumnRequest implements TableUpdateRequest {

    @Getter
    @JsonProperty("oldFieldName")
    private final String[] oldFieldName;

    @Getter
    @JsonProperty("newFieldName")
    private final String newFieldName;

    public RenameTableColumnRequest(String[] oldFieldName, String newFieldName) {
      this.oldFieldName = oldFieldName;
      this.newFieldName = newFieldName;
    }

    public RenameTableColumnRequest() {
      this(null, null);
    }

    @Override
    public void validate() throws IllegalArgumentException {
      Preconditions.checkArgument(
          oldFieldName != null
              && oldFieldName.length > 0
              && Arrays.stream(oldFieldName).allMatch(StringUtils::isNotBlank),
          "\"oldFieldName\" field is required and cannot be empty");
      Preconditions.checkArgument(
          StringUtils.isNotBlank(newFieldName),
          "\"newFieldName\" field is required and cannot be empty");
    }

    @Override
    public TableChange tableChange() {
      return TableChange.renameColumn(oldFieldName, newFieldName);
    }
  }

  @EqualsAndHashCode
  @ToString
  class UpdateTableColumnTypeRequest implements TableUpdateRequest {

    @Getter
    @JsonProperty("fieldName")
    private final String[] fieldName;

    @Getter
    @JsonProperty("newType")
    @JsonSerialize(using = JsonUtils.TypeSerializer.class)
    @JsonDeserialize(using = JsonUtils.TypeDeserializer.class)
    private final Type newType;

    public UpdateTableColumnTypeRequest(String[] fieldName, Type newType) {
      this.fieldName = fieldName;
      this.newType = newType;
    }

    public UpdateTableColumnTypeRequest() {
      this(null, null);
    }

    @Override
    public void validate() throws IllegalArgumentException {
      Preconditions.checkArgument(
          fieldName != null
              && fieldName.length > 0
              && Arrays.stream(fieldName).allMatch(StringUtils::isNotBlank),
          "\"fieldName\" field is required and cannot be empty");
      Preconditions.checkArgument(
          newType != null, "\"newType\" field is required and cannot be empty");
    }

    @Override
    public TableChange tableChange() {
      return TableChange.updateColumnType(fieldName, newType);
    }
  }

  @EqualsAndHashCode
  @ToString
  class UpdateTableColumnCommentRequest implements TableUpdateRequest {

    @Getter
    @JsonProperty("fieldName")
    private final String[] fieldName;

    @Getter
    @JsonProperty("newComment")
    private final String newComment;

    public UpdateTableColumnCommentRequest(String[] fieldName, String newComment) {
      this.fieldName = fieldName;
      this.newComment = newComment;
    }

    public UpdateTableColumnCommentRequest() {
      this(null, null);
    }

    @Override
    public void validate() throws IllegalArgumentException {
      Preconditions.checkArgument(
          fieldName != null
              && fieldName.length > 0
              && Arrays.stream(fieldName).allMatch(StringUtils::isNotBlank),
          "\"fieldName\" field is required and cannot be empty");
      Preconditions.checkArgument(
          StringUtils.isNotBlank(newComment),
          "\"newComment\" field is required and cannot be empty");
    }

    @Override
    public TableChange tableChange() {
      return TableChange.updateColumnComment(fieldName, newComment);
    }
  }

  @EqualsAndHashCode
  @ToString
  class UpdateTableColumnPositionRequest implements TableUpdateRequest {

    @Getter
    @JsonProperty("fieldName")
    private final String[] fieldName;

    @Getter
    @JsonProperty("newPosition")
    @JsonSerialize(using = JsonUtils.ColumnPositionSerializer.class)
    @JsonDeserialize(using = JsonUtils.ColumnPositionDeserializer.class)
    private final TableChange.ColumnPosition newPosition;

    public UpdateTableColumnPositionRequest(
        String[] fieldName, TableChange.ColumnPosition newPosition) {
      this.fieldName = fieldName;
      this.newPosition = newPosition;
    }

    public UpdateTableColumnPositionRequest() {
      this(null, null);
    }

    @Override
    public void validate() throws IllegalArgumentException {
      Preconditions.checkArgument(
          fieldName != null
              && fieldName.length > 0
              && Arrays.stream(fieldName).allMatch(StringUtils::isNotBlank),
          "\"fieldName\" field is required and cannot be empty");
      Preconditions.checkArgument(
          newPosition != null, "\"newPosition\" field is required and cannot be empty");
    }

    @Override
    public TableChange tableChange() {
      return TableChange.updateColumnPosition(fieldName, newPosition);
    }
  }

  @EqualsAndHashCode
  @ToString
  class DeleteTableColumnRequest implements TableUpdateRequest {

    @Getter
    @JsonProperty("fieldName")
    private final String[] fieldName;

    @Getter
    @JsonProperty("ifExists")
    private final boolean ifExists;

    public DeleteTableColumnRequest(String[] fieldName, boolean ifExists) {
      this.fieldName = fieldName;
      this.ifExists = ifExists;
    }

    public DeleteTableColumnRequest() {
      this(null, false);
    }

    @Override
    public void validate() throws IllegalArgumentException {
      Preconditions.checkArgument(
          fieldName != null
              && fieldName.length > 0
              && Arrays.stream(fieldName).allMatch(StringUtils::isNotBlank),
          "\"fieldName\" field is required and cannot be empty");
    }

    @Override
    public TableChange tableChange() {
      return TableChange.deleteColumn(fieldName, ifExists);
    }
  }
}