package net.bible.service.download;

import org.crosswire.jsword.book.Book;

public class RepoFactory {
	private CrosswireRepo crosswireRepo = new CrosswireRepo();
	
	private XiphosRepo xiphosRepo = new XiphosRepo();

	private BetaRepo betaRepo = new BetaRepo();

	private AndBibleRepo andBibleRepo = new AndBibleRepo();
	
	private static RepoFactory instance = new RepoFactory();
	private RepoFactory() {}
	public static RepoFactory getInstance() {
		return instance;
	}
	

	public RepoBase getRepoForBook(Book document) {
		String repoName = (String)document.getProperty(DownloadManager.REPOSITORY_KEY);

		RepoBase repoForBook;
		if (crosswireRepo.getRepoName().equals(repoName)) {
			repoForBook = crosswireRepo;
		} else if (xiphosRepo.getRepoName().equals(repoName)) {
			repoForBook = xiphosRepo;
		} else if (andBibleRepo.getRepoName().equals(repoName)) {
			repoForBook = andBibleRepo;
		} else if (betaRepo.getRepoName().equals(repoName)) {
			repoForBook = betaRepo;
		} else {
			repoForBook = crosswireRepo;
		}
		return repoForBook;
	}
	public CrosswireRepo getCrosswireRepo() {
		return crosswireRepo;
	}
	public XiphosRepo getXiphosRepo() {
		return xiphosRepo;
	}
	public BetaRepo getBetaRepo() {
		return betaRepo;
	}
	public AndBibleRepo getAndBibleRepo() {
		return andBibleRepo;
	}
}
