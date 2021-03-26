package tictim.paraglider.dialog.data;

import java.util.ArrayList;
import java.util.List;

public class DiffFile{
	public final List<Diff> diff = new ArrayList<>();

	public static DiffFile combine(List<DiffFile> diffFiles){
		if(diffFiles.isEmpty()) return new DiffFile();

		DiffFile diffFile = diffFiles.get(0);
		for(int i = 1; i<diffFiles.size(); i++){
			DiffFile diffFile1 = diffFiles.get(i);
			diffFile.diff.addAll(diffFile1.diff);
		}
		return diffFile;
	}
}
