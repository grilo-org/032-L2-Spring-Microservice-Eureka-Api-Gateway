#!/bin/bash

# Function to check if Docker is installed
check_docker() {
  if ! command -v docker &> /dev/null; then
    echo "Docker is not installed. Please install Docker first."
    exit 1
  fi

  if ! command -v docker-compose &> /dev/null; then
    echo "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
  fi
}

# Function to build and start services
start_services() {
  local build_flag=""
  local detached_flag="-d"
  
  # Check if we need to rebuild
  if [ "$1" = "rebuild" ]; then
    build_flag="--build"
    echo "Rebuilding all services..."
  fi

  # Check if we should run in foreground
  if [ "$2" = "foreground" ]; then
    detached_flag=""
    echo "Starting in foreground mode..."
  fi
  
  echo "Building and starting microservices..."
  docker-compose up $build_flag $detached_flag
  
  # If we're running in detached mode, wait a bit and show status
  if [ "$detached_flag" = "-d" ]; then
    echo "Waiting for services to start..."
    sleep 15
    
    echo "Checking services status:"
    docker-compose ps
    
    echo ""
    show_service_info
  fi
}

# Function to show service information
show_service_info() {
  echo "=== Service URLs ==="
  echo "Eureka Server: http://localhost:8761"
  echo "API Gateway: http://localhost:8090"
  echo "Swagger UI: http://localhost:8090/swagger-ui/index.html"
  echo "H2 Console: http://localhost:8080/h2-console"
  echo "H2 Credentials: JDBC URL=jdbc:h2:mem:orderdb, User=sa, Password=orderdb2025"
  echo ""
  echo "Test credentials:"
  echo "Admin: username=admin, password=admin123"
  echo "User: username=user, password=user123"
}

# Function to stop services
stop_services() {
  local remove_volumes=""
  
  if [ "$1" = "clean" ]; then
    remove_volumes="-v"
    echo "Stopping services and removing volumes..."
  else
    echo "Stopping services..."
  fi
  
  docker-compose down $remove_volumes
}

# Function to show logs
show_logs() {
  if [ -z "$1" ]; then
    echo "Showing logs for all services..."
    docker-compose logs -f
  else
    echo "Showing logs for service $1..."
    docker-compose logs -f $1
  fi
}

# Function to check health of services
check_health() {
  echo "Checking Eureka Server health..."
  curl -s http://localhost:8761/health || echo "Eureka Server not responding"
  
  echo ""
  echo "Checking API Gateway health..."
  curl -s http://localhost:8090/health || echo "API Gateway not responding"
  
  echo ""
  echo "Checking Order Service health..."
  curl -s http://localhost:8080/api/auth/health || echo "Order Service not responding"
}

# Function to restart a specific service
restart_service() {
  if [ -z "$1" ]; then
    echo "Please specify a service to restart"
    return 1
  fi
  
  echo "Restarting service $1..."
  docker-compose restart $1
  
  echo "Waiting for service to restart..."
  sleep 5
  
  echo "Service status:"
  docker-compose ps $1
}

# Function to generate a JWT token for testing
generate_test_token() {
  echo "Generating test JWT token..."
  echo "Requesting token for user: $1"
  
  curl -s -X POST \
    http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$1\",\"password\":\"$2\"}"
  
  echo ""
}

# Interactive menu function
show_menu() {
  clear
  echo "===== Order Packaging System - Microservices ====="
  echo "1. Start all services"
  echo "2. Start all services (rebuild containers)"
  echo "3. Start all services (foreground mode)"
  echo "4. Stop all services"
  echo "5. Stop all services and clean volumes"
  echo "6. View Eureka Server logs"
  echo "7. View API Gateway logs"
  echo "8. View Order Service logs"
  echo "9. View all services logs"
  echo "10. Check services status"
  echo "11. Check services health"
  echo "12. Restart Eureka Server"
  echo "13. Restart API Gateway"
  echo "14. Restart Order Service"
  echo "15. Generate test JWT token"
  echo "16. Show service URLs and credentials"
  echo "17. Exit"
  echo "Enter your option:"
  read option
}

# Main function
main() {
  # If no args provided, show interactive menu
  if [ $# -eq 0 ]; then
    # Check if Docker is installed
    check_docker
    
    show_menu
    
    # Process chosen option
    case $option in
      1)
        start_services
        ;;
      2)
        start_services "rebuild"
        ;;
      3)
        start_services "" "foreground"
        ;;
      4)
        stop_services
        ;;
      5)
        stop_services "clean"
        ;;
      6)
        show_logs "eureka-server"
        ;;
      7)
        show_logs "api-gateway"
        ;;
      8)
        show_logs "order-service"
        ;;
      9)
        show_logs
        ;;
      10)
        docker-compose ps
        ;;
      11)
        check_health
        ;;
      12)
        restart_service "eureka-server"
        ;;
      13)
        restart_service "api-gateway"
        ;;
      14)
        restart_service "order-service"
        ;;
      15)
        echo "Username [default:admin]:"
        read username
        username=${username:-admin}
        echo "Password [default:admin123]:"
        read password
        password=${password:-admin123}
        generate_test_token "$username" "$password"
        ;;
      16)
        show_service_info
        ;;
      17)
        echo "Exiting..."
        exit 0
        ;;
      *)
        echo "Invalid option!"
        ;;
    esac
  else
    # Command line arguments provided
    case "$1" in
      start)
        check_docker
        if [ "$2" = "rebuild" ]; then
          start_services "rebuild"
        elif [ "$2" = "foreground" ]; then
          start_services "" "foreground"
        else
          start_services
        fi
        ;;
      stop)
        check_docker
        if [ "$2" = "clean" ]; then
          stop_services "clean"
        else
          stop_services
        fi
        ;;
      logs)
        check_docker
        show_logs "$2"
        ;;
      status)
        check_docker
        docker-compose ps
        ;;
      health)
        check_health
        ;;
      restart)
        check_docker
        restart_service "$2"
        ;;
      info)
        show_service_info
        ;;
      token)
        username=${2:-admin}
        password=${3:-admin123}
        generate_test_token "$username" "$password"
        ;;
      help)
        echo "Usage: $0 [command] [options]"
        echo ""
        echo "Commands:"
        echo "  start [rebuild|foreground] - Start all services"
        echo "  stop [clean]               - Stop all services"
        echo "  logs [service-name]        - Show logs for services"
        echo "  status                     - Show status of services"
        echo "  health                     - Check health of services"
        echo "  restart [service-name]     - Restart a specific service"
        echo "  info                       - Show service URLs and credentials"
        echo "  token [username] [password] - Generate test JWT token"
        echo "  help                       - Show this help message"
        echo ""
        echo "Examples:"
        echo "  $0 start rebuild           - Start services and rebuild containers"
        echo "  $0 logs api-gateway        - Show API Gateway logs"
        echo "  $0 token user user123      - Generate token for user"
        ;;
      *)
        echo "Unknown command: $1"
        echo "Run '$0 help' for usage information"
        exit 1
        ;;
    esac
  fi
}

# Execute main function with all args
main "$@"
  6)
    show_logs
    ;;
  7)
    docker-compose ps
    ;;
  8)
    echo "Saindo..."
    exit 0
    ;;
  *)
    echo "Opção inválida!"
    ;;
esac
