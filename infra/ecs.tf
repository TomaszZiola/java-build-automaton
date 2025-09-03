resource "aws_ecs_cluster" "main" {
  name = "java-build-automaton-cluster"

  tags = {
    Name = "JBA Cluster"
  }
}

resource "aws_iam_role" "ecs_task_execution_role" {
  name = "jba-ecs-task-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_ecs_task_definition" "app_task" {
  family                   = "java-build-automaton-task"
  cpu                      = 256
  memory                   = 512
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  depends_on               = [aws_cloudwatch_log_group.ecs_logs]

  container_definitions = jsonencode([
    {
      name      = "java-build-automaton-container"
      image     = aws_ecr_repository.app_repository.repository_url
      essential = true
      portMappings = [
        {
          containerPort = 8080
          hostPort      = 8080
        }
      ]
      environment = [
        {
          name  = "SPRING_PROFILES_ACTIVE",
          value = "prod"
        },
        {
          name  = "DB_USERNAME",
          value = aws_db_instance.app_db.username
        },
        {
          name  = "DB_PASSWORD",
          value = random_password.db_password.result
        },
        {
          name  = "JDBC_CONNECTION_STRING",
          value = "jdbc:postgresql://${aws_db_instance.app_db.address}:${aws_db_instance.app_db.port}/${aws_db_instance.app_db.db_name}"
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = "/ecs/java-build-automaton"
          "awslogs-region"        = "eu-central-1"
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])

  tags = {
    Name = "JBA Task Definition"
  }
}

resource "aws_security_group" "app_sg" {
  name        = "jba-app-sg"
  description = "Allow HTTP traffic to the application"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "JBA App Security Group"
  }
}

resource "aws_ecs_service" "main" {
  name            = "java-build-automaton-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app_task.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = [aws_subnet.public_a.id, aws_subnet.public_b.id]
    security_groups  = [aws_security_group.app_sg.id]
    assign_public_ip = true
  }

  wait_for_steady_state = true

  tags = {
    Name = "JBA Service"
  }
}

resource "aws_cloudwatch_log_group" "ecs_logs" {
  name = "/ecs/java-build-automaton"

  tags = {
    Name = "JBA ECS Logs"
  }
}
