package in.odachi.douyucollector.database.entity;

public class Gift implements Entity {

    private Integer id = null;

    private String name = null;

    private String type = null;

    private Double pc = Double.MAX_VALUE;

    private Double gx = Double.MAX_VALUE;

    private String desc = null;

    private String intro = null;

    private String mimg = null;

    private String himg = null;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getPc() {
        return pc;
    }

    public void setPc(Double pc) {
        this.pc = pc;
    }

    public Double getGx() {
        return gx;
    }

    public void setGx(Double gx) {
        this.gx = gx;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getMimg() {
        return mimg;
    }

    public void setMimg(String mimg) {
        this.mimg = mimg;
    }

    public String getHimg() {
        return himg;
    }

    public void setHimg(String himg) {
        this.himg = himg;
    }
}
