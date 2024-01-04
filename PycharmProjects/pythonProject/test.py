import os

from pytube import YouTube

# 輸入 YouTube 視頻的 URL
video_url = "https://youtu.be/KQpbzrKbosE?si=McKVPY20RWn3JvC9"

# 創建 YouTube 對象
yt = YouTube(video_url)

# 選擇要下載的視頻流（這裡選擇最高品質的視頻流）
video_stream = yt.streams.get_highest_resolution()

# 指定下载路径
download_path = "C:\\Users\\User\\Desktop\\python video"
os.chdir(download_path)

print("開始下載！!")
# 開始下載

video_stream.download()
print("下載完成！!")