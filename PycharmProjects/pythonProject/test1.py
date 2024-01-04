from pytube import YouTube

def get_youtube_video_views(url):
    yt = YouTube(url)
    return yt.views

video_url = 'https://youtu.be/KQpbzrKbosE?si=McKVPY20RWn3JvC9'  # 替换为目标 YouTube 视频的 URL
views = get_youtube_video_views(video_url)
print(f"The video has {views} views.")