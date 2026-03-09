public class LibraryBookRecord {
    //Defining attributes
    private String oclcNumber;
    private String title;
    private String authors;
    private String summary;
    private int publicationYear;

    //Setter
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

    //Getter
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

    @Override
    public String toString() {
        // We build a single string that combines all the book's details
        return "Book Details:\n" +
                "------------------------------\n" +
                "OCLC: " + oclcNumber + "\n" +
                "Title: " + title + "\n" +
                "Author: " + authors + "\n" +
                "Year: " + publicationYear + "\n" +
                "Summary: " + summary + "\n" +
                "------------------------------";
    }
}
