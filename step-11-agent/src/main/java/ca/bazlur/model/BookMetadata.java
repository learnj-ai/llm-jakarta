package ca.bazlur.model;

import java.util.Date;

public record BookMetadata(
    Date createdAt,
    String targetAge,
    int pageCount,
    String educationalGoals,
    String illustrationStyle,
    String mainCharacter
) {
    public static BookMetadataBuilder builder() {
        return new BookMetadataBuilder();
    }

    public static class BookMetadataBuilder {
        private Date createdAt;
        private String targetAge;
        private int pageCount;
        private String educationalGoals;
        private String illustrationStyle;
        private String mainCharacter;

        public BookMetadataBuilder createdAt(Date createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BookMetadataBuilder targetAge(String targetAge) {
            this.targetAge = targetAge;
            return this;
        }

        public BookMetadataBuilder pageCount(int pageCount) {
            this.pageCount = pageCount;
            return this;
        }

        public BookMetadataBuilder educationalGoals(String educationalGoals) {
            this.educationalGoals = educationalGoals;
            return this;
        }

        public BookMetadataBuilder illustrationStyle(String illustrationStyle) {
            this.illustrationStyle = illustrationStyle;
            return this;
        }

        public BookMetadataBuilder mainCharacter(String mainCharacter) {
            this.mainCharacter = mainCharacter;
            return this;
        }

        public BookMetadata build() {
            return new BookMetadata(createdAt, targetAge, pageCount, educationalGoals, illustrationStyle, mainCharacter);
        }
    }
}