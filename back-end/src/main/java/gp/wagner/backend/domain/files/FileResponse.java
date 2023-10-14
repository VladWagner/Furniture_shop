package gp.wagner.backend.domain.files;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

//Похоже, то это класс-рудимент, поскольку нигде он не используется и не очень понятно, для чего он нужен
@Getter
@Setter
@AllArgsConstructor
public class FileResponse {

    private String fileName;
    private String fileUri;
    private String fileType;
    private long fileSize;

}
