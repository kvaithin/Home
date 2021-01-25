package com.softserveinc.ita.homeproject.homeservice.service.impl;

import com.softserveinc.ita.homeproject.homedata.entity.News;
import com.softserveinc.ita.homeproject.homedata.repository.NewsRepository;
import com.softserveinc.ita.homeproject.homeservice.dto.NewsDto;
import com.softserveinc.ita.homeproject.homeservice.exception.NotFoundHomeException;
import com.softserveinc.ita.homeproject.homeservice.mapper.EntityMapper;
import com.softserveinc.ita.homeproject.homeservice.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public NewsDto create(NewsDto newsDto) {
        News news = mapper.convertToEntity(newsDto, new News());
        news.setCreateDate(LocalDateTime.now());

        newsRepository.save(news);

        return mapper.convertToDto(news, new NewsDto());
    }

    @Override
    @Transactional
    public NewsDto update(Long id, NewsDto newsDto)  {

        if (newsRepository.findById(id).isPresent()) {

            News fromDB = newsRepository.findById(id).get();

            if (newsDto.getTitle() != null) {
                fromDB.setTitle(newsDto.getTitle());
            }

            if (newsDto.getText() != null) {
                fromDB.setText(newsDto.getText());
            }

            if (newsDto.getDescription() != null) {
                fromDB.setDescription(newsDto.getDescription());
            }

            if (newsDto.getPhotoUrl() != null) {
                fromDB.setPhotoUrl(newsDto.getPhotoUrl());
            }

            if (newsDto.getSource() != null) {
                fromDB.setSource(newsDto.getSource());
            }

            fromDB.setUpdateDate(LocalDateTime.now());
            newsRepository.save(fromDB);
            return mapper.convertToDto(fromDB, new NewsDto());

        } else {
            throw new NotFoundHomeException("Can't find news with given ID:" + id);
        }
    }

    @Override
    public Page<NewsDto> getAll(Integer pageNumber, Integer pageSize) {
        return newsRepository.findAll(PageRequest.of(pageNumber - 1, pageSize))
                .map((news) -> mapper.convertToDto(news, new NewsDto()));
    }

    @Override
    public NewsDto getById(Long id) {
        News newsResponse = newsRepository.findById(id)
                .orElseThrow(() -> new NotFoundHomeException("Can't find news with given ID:" + id));

        return mapper.convertToDto(newsResponse, new NewsDto());
    }

    @Override
    public void deleteById(Long id) {
        newsRepository.findById(id)
                .orElseThrow(() -> new NotFoundHomeException("Can't find news with given ID:" + id));
        newsRepository.deleteById(id);
    }

}
