package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name,mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
       Artist artist = null;
         for (Artist a : artists){
             if(artist.getName().equals(artistName)){
                artist = a;
             }
         }
         if (artist == null){
             Artist art = new Artist(artistName);
             artists.add(art);
         }
        Album album = new Album(title);
        albums.add(album);

        List<Album> artistAlbums = artistAlbumMap.getOrDefault(artist, new ArrayList<>());
        artistAlbums.add(album);
        artistAlbumMap.put(artist, artistAlbums);

        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Album album = getAlbumByName(albumName);
        if (album == null){
            throw new Exception("Album does not exist");
        }
        Song song = new Song(title,length);
        songs.add(song);

        List<Song> songList = albumSongMap.getOrDefault(album,new ArrayList<>());
        songList.add(song);
        albumSongMap.put(album,songList);
        return song;

    }
    private Album getAlbumByName(String name){
        Album album = null;
        for (Album a : albums){
            if(a.getTitle().equals(name)){
                album = a;
                break;
            }
        }
        return album;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
         User user = getUserByMobile(mobile);
         if (user == null){
             throw new Exception("User does not exist");
         }
         Playlist playlist = new Playlist(title);
         playlists.add(playlist);

         List<Song> playListSongs = new ArrayList<>();
         for (Song song : songs){
             if (song.getLength() == length){
                 playListSongs.add(song);
             }
         }
         playlistSongMap.put(playlist,playListSongs);

         List<User> listners = new ArrayList<>();
         listners.add(user);
         playlistListenerMap.put(playlist,listners);
         creatorPlaylistMap.put(user,playlist);

         List<Playlist> userPlayList = userPlaylistMap.getOrDefault(user,new ArrayList<>());
         userPlayList.add(playlist);
         userPlaylistMap.put(user,userPlayList);
         return playlist;

    }

    private User getUserByMobile(String mobile){
        User user = null;
        for (User u : users){
            if (u.getMobile().equals(mobile)){
                user = u;
                break;
            }
        }
        return  user;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {

        User user = getUserByMobile(mobile);
        if (user == null){
            throw new Exception("User does not exist");
        }
        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        List<Song> playListSong = new ArrayList<>();
        for (Song song : songs){
            if (songTitles.contains(song.getTitle())){
                playListSong.add(song);
            }
        }

        playlistSongMap.put(playlist,playListSong);

        List<User> listners = new ArrayList<>();
        listners.add(user);
        playlistListenerMap.put(playlist,listners);
        creatorPlaylistMap.put(user,playlist);

        List<Playlist> userPlayList = userPlaylistMap.getOrDefault(user,new ArrayList<>());
        userPlayList.add(playlist);
        userPlaylistMap.put(user,userPlayList);
        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
          User user = getUserByMobile(mobile);
          if (user == null){
              throw new Exception("User does not exist");
          }
          Playlist playlist = getPlaylistByTitle(playlistTitle);
          if (playlist == null){
              throw new Exception("PlayList does not exist");
          }

          List<User> ListOfListners = playlistListenerMap.getOrDefault(playlist,new ArrayList<>());
          if (!ListOfListners.contains(user)){
              ListOfListners.add(user);
              playlistListenerMap.put(playlist,ListOfListners);
          }
          return playlist;
    }

    private Playlist getPlaylistByTitle(String Title){
        Playlist playlist = null;

        for (Playlist p : playlists){
            if (p.getTitle().equals(Title)){
                playlist = p;
                break;
            }
        }
        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
       User user = getUserByMobile(mobile);
       if (user == null){
           throw new Exception("User does not exist");
       }
       Song song =null;
       for (Song s :songs){
           if (s.getTitle().equals(songTitle)){
               song = s;
               break;
           }
       }
       if (song == null){
           throw new Exception("Song does not exist");
       }

       List<User> likeForSong = songLikeMap.getOrDefault(song,new ArrayList<>());
       if (!likeForSong.contains(user)){
           likeForSong.add(user);
           songLikeMap.put(song,likeForSong);

           Artist artist = getArtistBySong(song);
           if (artist != null){
               artist.setLikes(artist.getLikes()+1);
           }

           song.setLikes(song.getLikes()+1);
       }
       return song;
    }

    private Artist getArtistBySong(Song song) {
        for (Map.Entry<Artist, List<Album>> entry : artistAlbumMap.entrySet()) {
            List<Album> albums = entry.getValue();
            for (Album album : albums) {
                if (albumSongMap.containsKey(album) && albumSongMap.get(album).contains(song)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public String mostPopularArtist() {
        int maxLikes = Integer.MIN_VALUE;
        Artist artist = null;
        for (Artist a : artists){
            if (maxLikes<a.getLikes()){
                maxLikes = a.getLikes();
                artist = a;
            }
        }
        return artist.getName();
    }

    public String mostPopularSong() {
        int maxLikes = Integer.MIN_VALUE;
        Song song = null;
        for (Song s : songs){
            if (maxLikes<s.getLikes()){
                maxLikes = s.getLikes();
                song = s;
            }
        }
        return song.getTitle();
    }
}
