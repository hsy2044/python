from pytube import YouTube

def get_youtube_video_views(url):
    yt = YouTube(url)
    return yt.views

video_url = 'https://www.youtube.com/watch?v=EdxSS-1c-cU'  # 替换为目标 YouTube 视频的 URL
views = get_youtube_video_views(video_url)
print(f"The video has {views} views.")