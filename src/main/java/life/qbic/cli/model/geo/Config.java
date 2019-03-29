package life.qbic.cli.model.geo;


public class Config {

    private String app;
    private String dss;
    private String username;
    private String password;
    private String organism;
    private String source_name;
    private String source_name_detailed;
    private String title;
    private String molecule;
    private String characteristics;
    private String property;
    private String experiment;

    public Config(String app, String dss, String username, String password, String organism, String source_name, String soruce_name_detailed, String title, String molecule, String characteristics, String property, String experiment) {
        this.app = app;
        this.dss = dss;
        this.username = username;
        this.password = password;
        this.organism = organism;
        this.source_name = source_name;
        this.source_name_detailed = soruce_name_detailed;
        this.title = title;
        this.molecule = molecule;
        this.characteristics = characteristics;
        this.property = property;
        this.experiment = experiment;
    }

    public Config() {

    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getDss() {
        return dss;
    }

    public void setDss(String dss) {
        this.dss = dss;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOrganism() {
        return organism;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public String getSource_name() {
        return source_name;
    }

    public void setSource_name(String source_name) {
        this.source_name = source_name;
    }

    public String getSource_name_detailed() {
        return source_name_detailed;
    }

    public void setSource_name_detailed(String source_name_detailed) {
        this.source_name_detailed = source_name_detailed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMolecule() {
        return molecule;
    }

    public void setMolecule(String molecule) {
        this.molecule = molecule;
    }

    public String getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(String characteristics) {
        this.characteristics = characteristics;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getExperiment() {
        return experiment;
    }

    public void setExperiment(String experiment) {
        this.experiment = experiment;
    }
}