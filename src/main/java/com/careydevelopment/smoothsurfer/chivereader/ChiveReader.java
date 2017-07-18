package com.careydevelopment.smoothsurfer.chivereader;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.careydevelopment.smoothsurfer.entity.Images;
import com.careydevelopment.smoothsurfer.repository.ImagesRepository;

@Component
public class ChiveReader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ChiveReader.class);
	
	private LinkRetriever.FetchType fetchTypes[] = {LinkRetriever.FetchType.AFTERNOON_RANDOMNESS, LinkRetriever.FetchType.MORNING_AWESOMENESS};	

	private boolean persistedImages = false;
	
	@Autowired
	ImagesRepository repository;
	
	
	public void launch() {
		try {
			for (LinkRetriever.FetchType fetchType : fetchTypes) {
				persistedImages = false;
				
				LinkRetriever retriever = new LinkRetriever(fetchType);
				
				List<String> urls = retriever.getLinks();
				urls.forEach((link) -> {
					if (!persistedImages) getImagesFromLink(link);
				});
				
				String nextPageLink = retriever.getNextPageLink();
				if (nextPageLink != null) {
					retriever = new LinkRetriever(nextPageLink, fetchType);
				} else {
					retriever = null;
				}
			} 
		} catch (Exception e) {
			LOGGER.error("Problem traversing links!", e);
		} 
	}
	
	
	private void getImagesFromLink(String link) {
		LOGGER.debug("Looking at link " + link);

		try {
			ImageRetriever retriever = new ImageRetriever(new URL(link));
			List<String> images = retriever.getImages();
			
			images.forEach((image) -> {
				persistImage(image, retriever.getDate());
			});
		} catch (Exception e) {
			LOGGER.error("Problem retrieving images from link " + link, e);
		}
	}
	
	
	private void persistImage(String image, Date date) {
		LOGGER.debug("checking on " + image);

		Images foundImage = repository.findByImage(image);
		
		if (foundImage == null) {
			Images im = new Images();
			im.setCategory("Funny");
			im.setDate(date);
			im.setImage(image);
			im.setKeys(new ArrayList<String>());
			
			repository.save(im);
		
			LOGGER.debug("persisted " + image);
			
			persistedImages = true;
		}
	}
}
