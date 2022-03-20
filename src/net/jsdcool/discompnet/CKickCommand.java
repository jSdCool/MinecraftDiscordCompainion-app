package net.jsdcool.discompnet;

public class CKickCommand extends CDataType{
    private static final long serialVersionUID = 1L;
    public String name,reason;
    public CKickCommand(String name,String reason){
        this.name=name;
        this.reason=reason;
    }
}
