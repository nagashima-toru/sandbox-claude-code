package com.sandbox.api.presentation.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 
 * RFC 7807 Problem Details for HTTP APIs
 * エラー情報を標準化された形式で提供します。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

  /** エラーの種類を識別するURI（デフォルト: about:blank） */
  private String type;

  /** エラーの概要（人間が読める形式） */
  private String title;

  /** HTTPステータスコード */
  private int status;

  /** エラーの詳細説明 */
  private String detail;

  /** エラーが発生したリクエストのURI */
  private String instance;

  /** エラー発生日時（ISO 8601形式） */
  private LocalDateTime timestamp;

  /** バリデーションエラーの詳細リスト（バリデーションエラーの場合のみ） */
  private List<ValidationError> errors;

  /**
   * バリデーションエラーの詳細
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ValidationError {
    /** エラーが発生したフィールド名 */
    private String field;

    /** 拒否された値 */
    private Object rejectedValue;

    /** エラーメッセージ */
    private String message;
  }
}
