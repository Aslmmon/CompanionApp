# Code Naming Conventions

These rules define standard naming conventions across architectural components.

---

## 1. Component Suffixes
* **ViewModels**: `<Feature>ViewModel` (e.g., `HomeViewModel`)
* **Stateful Screen Wrappers**: `<Feature>Screen` (e.g., `HomeScreen`)
* **Stateless Screen Contents**: `<Feature>Content` (e.g., `HomeContent`)
* **One-Shot UI Events**: `<Feature>UiEvent` (e.g., `HomeUiEvent`)
* **Use Cases**: `<Verb><Subject>UseCase` (e.g., `GetTodayJourneyUseCase`)
* **Repositories**: 
  - Interface: `<Subject>Repository` (e.g., `JourneyRepository`)
  - Implementation: `<Subject>RepositoryImpl` (e.g., `JourneyRepositoryImpl`)
* **Data Transfer Objects**: `<Model>Dto` (e.g., `JourneyDto`)
* **Data Sources**:
  - Interface: `<Source>LocalDataSource` or `<Source>RemoteDataSource`
  - Implementation: `Resource<Source>LocalDataSource` or similar
