public class LibraryBookRecord {
    // Defining attributes
    private String oclcNumber;
    private String title;
    private String authors;
    private String summary;
    private int publicationYear;
    private String genre; // NEW: Added Genre

    // Setters
    public void setOclcNumber(String oclcNumber) {
        this.oclcNumber = oclcNumber;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    // Getters
    public int getPublicationYear() {
        return publicationYear;
    }

    public String getAuthors() {
        return authors;
    }

    public String getOclcNumber() {
        return oclcNumber;
    }

    public String getSummary() {
        return summary;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    @Override
    public String toString() {
        // We build a single string that combines all the book's details
        return "Book Details:\n" +
                "------------------------------\n" +
                "OCLC: " + oclcNumber + "\n" +
                "Title: " + title + "\n" +
                "Author(s): " + authors + "\n" +
                "Genre: " + genre + "\n" +
                "Year: " + publicationYear + "\n" +
                "Summary: " + summary + "\n" +
                "------------------------------\n";
    }
}
