package model;

import java.time.LocalDate;

/**
 * Person este un User plus:
 * nume, prenume, data nasteri, ocupatie, nivel de empatie
 */
public class Person extends User {
    private String nume;
    private String prenume;
    private LocalDate dataNasterii;
    private String ocupatie;
    private double nivelEmpatie;

    public Person(Long id, String username, String email, String password,
                  String nume, String prenume, LocalDate dataNasterii,
                  String ocupatie, double nivelEmpatie) {
        super(id, username, email, password);
        this.nume = nume;
        this.prenume = prenume;
        this.dataNasterii = dataNasterii;
        this.ocupatie = ocupatie;
        this.nivelEmpatie = nivelEmpatie;
    }

    public String getNume() { return nume; }
    public String getPrenume() { return prenume; }
    public LocalDate getDataNasterii() { return dataNasterii; }
    public String getOcupatie() { return ocupatie; }
    public double getNivelEmpatie() { return nivelEmpatie; }

    public void setNume(String nume) { this.nume = nume; }
    public void setPrenume(String prenume) { this.prenume = prenume; }
    public void setDataNasterii(LocalDate dataNasterii) { this.dataNasterii = dataNasterii; }
    public void setOcupatie(String ocupatie) { this.ocupatie = ocupatie; }
    public void setNivelEmpatie(double nivelEmpatie) { this.nivelEmpatie = nivelEmpatie; }
}
