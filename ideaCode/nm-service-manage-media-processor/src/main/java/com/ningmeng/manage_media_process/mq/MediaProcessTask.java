package com.ningmeng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.ningmeng.framework.domain.media.MediaFile;
import com.ningmeng.framework.domain.media.MediaFileProcess_m3u8;
import com.ningmeng.framework.exception.CustomExceptionCast;
import com.ningmeng.framework.model.response.CommonCode;
import com.ningmeng.framework.utils.HlsVideoUtil;
import com.ningmeng.framework.utils.Mp4VideoUtil;
import com.ningmeng.manage_media_process.dao.MediaFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by 12699 on 2020/3/5.
 */
@Component
public class MediaProcessTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaProcessTask.class);
    //ffmpeg绝对路径
    @Value("${nm-service-manage-media.ffmpeg-path}")
    String ffmpegPath;
    //上传文件根目录
    @Value("${nm-service-manage-media.upload-location}")
    private  String videoPath;
    @Autowired
    private MediaFileRepository mediaFileRepository;
    @RabbitListener(queues = "${nm-service-manage-media.mq.queue-media-processtask}",containerFactory="customContainerFactory")
    public void receiveMediaProcessTask(String msg){
        Map msgMap = JSON.parseObject(msg, Map.class);
        //媒资文件id
        String mediaId = (String) msgMap.get("mediaId");
        //获取媒资文件信息
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if(!optional.isPresent()){
            CustomExceptionCast.cast(CommonCode.FAIL);
            LOGGER.error("视频处理对象不能为空");
            return;
        }
        MediaFile mediaFile = optional.get();
        //媒资文件类型
        String fileType = mediaFile.getFileType();
        if(fileType == null || !fileType.equals("avi")){//目前只处理avi文件
            mediaFile.setProcessStatus("303004");//处理状态为无需处理
            mediaFileRepository.save(mediaFile);
            return ;
        }else{
            mediaFile.setProcessStatus("303001");//处理状态为未处理
            mediaFileRepository.save(mediaFile);
        }
            String mp4folderPath=null;
        String mp4Name=null;
        if("303001".equals(mediaFile.getProcessStatus())){
            //生成mp4
            String video_path = videoPath + mediaFile.getFilePath()+mediaFile.getFileName();
            mp4folderPath = videoPath + mediaFile.getFilePath();
            Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegPath,video_path,mp4Name,mp4folderPath);
            String result = mp4VideoUtil.generateMp4();
            if(result == null || !result.equals("success")){
                //操作失败写入处理日志
                mediaFile.setProcessStatus("303003");//处理状态为处理失败
                MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
                mediaFileProcess_m3u8.setErrormsg(result);
                mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
                mediaFileRepository.save(mediaFile);
                return ;
            }

        }
        String mp4FileVideoPath =mp4folderPath+mp4Name;//此地址为mp4的地址
        String m3u8_name = mediaFile.getFileId()+".m3u8";
        String m3u8folder_path = videoPath + mediaFile.getFilePath()+"hls/";
        File files = new File(m3u8folder_path);
        if(!files.exists()){
            files.mkdirs();
        }
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpegPath,videoPath,m3u8_name,m3u8folder_path);
        String result = hlsVideoUtil.generateM3u8();
        if(result == null || !result.equals("success")){
            //操作失败写入处理日志
            mediaFile.setProcessStatus("303003");//处理状态为处理失败
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return ;
        }
        mediaFile.setProcessStatus("303002");//处理状态为处理成功
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        mediaFile.setFileUrl(mediaFile.getFilePath()+"hls/"+m3u8_name);
        mediaFileRepository.save(mediaFile);


    }
}
