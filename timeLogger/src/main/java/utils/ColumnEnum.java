package utils;

public interface ColumnEnum<T extends Enum<T>> {
    String getColumnName();
}