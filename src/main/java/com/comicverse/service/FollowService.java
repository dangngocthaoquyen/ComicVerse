package com.comicverse.service;

import com.comicverse.model.Comic;
import com.comicverse.model.User;
import com.comicverse.repository.ComicRepository;
import com.comicverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {
    @Autowired private UserRepository userRepository;
    @Autowired private ComicRepository comicRepository;

    @Transactional
    public boolean toggleFollow(Long userId, Long comicId) {
        User user = userRepository.findById(userId).orElseThrow();
        Comic comic = comicRepository.findById(comicId).orElseThrow();

        if (user.getFollowedComics().contains(comic)) {
            // Nếu đã theo dõi thì xóa đi (Unfollow)
            user.getFollowedComics().remove(comic);
            comic.setFollowCount(Math.max(0, comic.getFollowCount() - 1));
            userRepository.save(user);
            return false; // Trạng thái hiện tại: Chưa theo dõi
        } else {
            // Nếu chưa thì thêm vào (Follow)
            user.getFollowedComics().add(comic);
            comic.setFollowCount(comic.getFollowCount() + 1);
            userRepository.save(user);
            return true; // Trạng thái hiện tại: Đang theo dõi
        }
    }
}