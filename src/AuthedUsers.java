import java.io.Serializable;
import java.util.ArrayList;
/**the point of this class is to store and load all the authed users ids
 * 
 * @author jSdCool
 *
 */
public class AuthedUsers implements Serializable {

	
	private static final long serialVersionUID = 1L;
	public ArrayList<String> ids=new ArrayList<>();
}
