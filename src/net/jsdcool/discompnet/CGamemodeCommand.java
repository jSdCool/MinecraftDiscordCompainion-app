package net.jsdcool.discompnet;

public class CGamemodeCommand extends CDataType{
    private static final long serialVersionUID = 1L;
    public String name,gameMode;
    public CGamemodeCommand(String name,String gameMode){
        this.name=name;
        this.gameMode=gameMode;
    }
}
